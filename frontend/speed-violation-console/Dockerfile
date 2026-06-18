FROM node:20-alpine
WORKDIR /app

COPY index.html generate-config.mjs ./

RUN npm install -g serve

ENV API_URL=http://host.docker.internal:8080

EXPOSE 3000

CMD ["sh", "-c", "node generate-config.mjs && serve . -p 3000"]
