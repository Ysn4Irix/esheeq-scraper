FROM --platform=arm64 node:18-alpine
WORKDIR /app
COPY ./client/package.json ./
RUN npm install --location=global pnpm
RUN pnpm install
COPY ./client .
RUN pnpm run build && pnpm prune --prod
EXPOSE 5173
CMD ["node", "build"]
