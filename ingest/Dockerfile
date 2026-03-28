FROM python:3.12-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY src/ ./src/
ENV PYTHONPATH=/app/src
CMD ["python", "src/telegram_listener.py"]

# Required environment variables at runtime:
# TELEGRAM_API_ID
# TELEGRAM_API_HASH
# KAFKA_BROKER
# TELEGRAM_CHANNEL
