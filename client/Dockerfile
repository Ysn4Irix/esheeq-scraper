FROM node:18-alpine
WORKDIR /app
COPY ./client/package.json ./client/pnpm-lock.yaml* ./client/pnpm-workspace.yaml* ./client/.npmrc* ./
RUN npm install --location=global pnpm
RUN pnpm install
COPY ./client .
RUN pnpm run build && pnpm prune --prod
ENV HOST=0.0.0.0
ENV PORT=5173
EXPOSE 5173
CMD ["node", "build"]
