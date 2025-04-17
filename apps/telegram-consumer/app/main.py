from fastapi import FastAPI, Request
import telegram
import os
import logging

app = FastAPI()
logging.basicConfig(level=logging.INFO)

TELEGRAM_TOKEN = os.getenv("TELEGRAM_TOKEN")
CHAT_ID = os.getenv("TELEGRAM_CHAT_ID")  # Например, свой user_id

bot = telegram.Bot(token=TELEGRAM_TOKEN)

@app.post("/send")
async def send(request: Request):
    data = await request.json()
    message = data.get("message", "<пусто>")

    try:
        bot.send_message(chat_id=CHAT_ID, text=message)
        return {"status": "ok", "sent": message}
    except Exception as e:
        logging.error(f"Ошибка при отправке в Telegram: {e}")
        return {"status": "error", "reason": str(e)}
