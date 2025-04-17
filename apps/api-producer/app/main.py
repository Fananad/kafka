from fastapi import FastAPI, Request, HTTPException
import httpx
import os
import logging
from datetime import datetime
import traceback

app = FastAPI()

# Конфигурация логгера
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",  # Включаем время
)

# URL telegram-consumer
TELEGRAM_CONSUMER_URL = os.getenv("TELEGRAM_CONSUMER_URL", "http://172.16.0.21:9000/send")


@app.post("/send")
async def send_message(request: Request):
    try:
        data = await request.json()
        message = data.get("message")

        if not message:
            raise HTTPException(status_code=400, detail="Missing 'message' field")

        logging.info("📥 Получено сообщение от клиента: %s", message)
        logging.info("📨 Отправляем в telegram-consumer по адресу: %s", TELEGRAM_CONSUMER_URL)

        async with httpx.AsyncClient() as client:
            response = await client.post(TELEGRAM_CONSUMER_URL, json={"message": message})
            response.raise_for_status()
            telegram_response = response.json()

        logging.info("📬 Ответ от telegram-consumer: %s", telegram_response)

        return {
            "status": "forwarded",
            "message": message,
            "response_from_consumer": telegram_response
        }

    except httpx.RequestError as e:
        logging.error("❌ Ошибка соединения с telegram-consumer: %s", e)
        raise HTTPException(status_code=502, detail="Cannot reach telegram-consumer")

    except Exception as e:
        logging.error("❌ Внутренняя ошибка:\n%s", traceback.format_exc())
        raise HTTPException(status_code=500, detail="Internal server error")
