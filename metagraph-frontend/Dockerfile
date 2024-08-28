# Build stage
FROM node:18 AS build

# Set working directory
WORKDIR /app

# Copy package.json and yarn.lock (if available)
COPY package*.json ./

# Install dependencies
RUN yarn install

# Copy all files
COPY . .

# Build the app
RUN yarn build

# Production stage
FROM nginx:alpine

# Copy built assets from build stage
COPY --from=build /app/build /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
