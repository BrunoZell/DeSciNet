# Build stage
FROM node:18 AS build

WORKDIR /app
COPY package*.json ./
COPY yarn.lock ./

# Set the cache directory for yarn
ENV YARN_CACHE_FOLDER=/root/.cache/yarn

# Use --mount=type=cache to cache yarn packages
RUN --mount=type=cache,target=$YARN_CACHE_FOLDER yarn install

COPY . .

RUN yarn build

# Production stage
FROM node:18

WORKDIR /app

COPY --from=build /app/package*.json ./
COPY --from=build /app/yarn.lock ./
COPY --from=build /app/.next ./.next

# Set the cache directory for yarn
ENV YARN_CACHE_FOLDER=/root/.cache/yarn

# Use --mount=type=cache to cache yarn packages
RUN --mount=type=cache,target=$YARN_CACHE_FOLDER yarn install --production

EXPOSE 3000
CMD ["yarn", "start"]
