from fastapi import FastAPI, Request
import logging
from datetime import datetime

app = FastAPI()

# Настройка логгера с датой и временем
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s"
)

@app.post("/send")
async def receive_message(request: Request):
    data = await request.json()
    message = data.get("message", "<нет message>")

    logging.info("📨 Получено сообщение от api-producer: %s", message)

    return {"status": "ok", "received": message}
