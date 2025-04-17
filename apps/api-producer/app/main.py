from fastapi import FastAPI, Request, HTTPException
import httpx
import os
import logging

app = FastAPI()
logging.basicConfig(level=logging.INFO)

# URL сервиса telegram-consumer, куда будем пересылать запрос
TELEGRAM_CONSUMER_URL = os.getenv("TELEGRAM_CONSUMER_URL", "http://localhost:9000/send")


@app.post("/send")
async def send_message(request: Request):
    """
    Обрабатывает входящий POST-запрос и пересылает сообщение в telegram-consumer
    """
    try:
        data = await request.json()
        message = data.get("message")

        if not message:
            raise HTTPException(status_code=400, detail="Missing 'message' field")

        logging.info(f"📥 Получено сообщение: {message}")

        async with httpx.AsyncClient() as client:
            response = await client.post(TELEGRAM_CONSUMER_URL, json={"message": message})
            response.raise_for_status()
            telegram_response = response.json()

        logging.info(f"📤 Переслано в telegram-consumer: {telegram_response}")
        return {
            "status": "forwarded",
            "message": message,
            "response_from_consumer": telegram_response
        }

    except httpx.RequestError as e:
        logging.error(f"❌ Ошибка соединения с telegram-consumer: {e}")
        raise HTTPException(status_code=502, detail="Cannot reach telegram-consumer")

    except Exception as e:
        logging.error(f"❌ Ошибка: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")
