from fastapi import FastAPI, Request
import logging

app = FastAPI()
logging.basicConfig(level=logging.INFO)

@app.post("/send")
async def send(request: Request):
    data = await request.json()
    logging.info(f"Получен запрос: {data}")
    # Тут позже будет отправка в Kafka
    return {"status": "ok", "received": data}
