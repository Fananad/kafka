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
    message = data.get("message")

    if not message:
        raise HTTPException(status_code=400, detail="Missing 'message' field")

    payload = {
        "chat_id": CHAT_ID,
        "text": message
    }

    try:
        async with httpx.AsyncClient() as client:
            response = await client.post(TELEGRAM_API_URL, json=payload)
            response.raise_for_status()
        log.info(f"✅ Сообщение отправлено в Telegram: {message}")
        return {"status": "ok", "telegram_response": response.json()}
    except httpx.HTTPError as e:
        log.error(f"❌ Ошибка при отправке в Telegram: {e}")
        raise HTTPException(status_code=502, detail="Failed to send to Telegram")
