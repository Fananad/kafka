from fastapi import FastAPI, Request, HTTPException
import httpx
import os
import logging

app = FastAPI()

# Настройка логов
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
log = logging.getLogger(__name__)

# Токен и chat_id передаются через переменные окружения
BOT_TOKEN = os.getenv("BOT_TOKEN")
CHAT_ID = os.getenv("CHAT_ID")

TELEGRAM_API_URL = f"https://api.telegram.org/bot{BOT_TOKEN}/sendMessage"

@app.post("/send")
async def send_to_telegram(request: Request):
    data = await request.json()
    log.info(f"🔍 Получен JSON: {data}")
    message = data.get("message")
    log.info(f"📨 Извлечено поле message: {message}")

    payload = {
        "chat_id": CHAT_ID,
        "text": message
    }
    log.info(f"📤 Готовим отправку в Telegram: {payload}")
    log.info(f"📡 URL для отправки: {TELEGRAM_API_URL}")
    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(TELEGRAM_API_URL, json=payload)
            log.info(f"📬 Ответ Telegram API: {response.status_code} - {response.text}")
            response.raise_for_status()
        log.info(f"✅ Сообщение отправлено в Telegram: {message}")
        return {"status": "ok", "telegram_response": response.json()}
    except httpx.HTTPStatusError as e:
        log.error(f"❌ Ошибка от Telegram API: {e.response.status_code} - {e.response.text}")
        raise HTTPException(status_code=502, detail="Telegram API returned error")
    except Exception as e:
        log.error(f"❌ Ошибка при отправке в Telegram: {str(e)}")
        raise HTTPException(status_code=500, detail="Internal error")
