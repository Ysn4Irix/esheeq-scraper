FROM --platform=arm64 node:18-alpine
ARG REDIS_PORT=${REDIS_PORT}
ENV REDIS_PORT=${REDIS_PORT}
ARG REDIS_HOST=${REDIS_HOST}
ENV REDIS_HOST=${REDIS_HOST}
ARG REDIS_PASSWORD=${REDIS_PASSWORD}
ENV REDIS_PASSWORD=${REDIS_PASSWORD}

WORKDIR /app
COPY ./api/package.json .
RUN npm install --location=global pnpm
RUN pnpm install
COPY ./api .
EXPOSE 4000
CMD ["node", "server.js"]