# =============================================================
# MICOS HAKARI — Frontend (React + Vite)
# Build multi-etapa: compila con Node y sirve con Nginx.
# =============================================================

# --- Etapa 1: Build ---
FROM node:20-alpine AS build
WORKDIR /app

# URL de la API (se inyecta en tiempo de build; default: /api vía Nginx)
ARG VITE_API_URL=/api
ENV VITE_API_URL=$VITE_API_URL

# Copia package.json y lockfile para aprovechar caché
COPY frontend/package.json ./
RUN npm install

# Copia el código fuente
COPY frontend/ .

# Compila la app (genera dist/)
RUN npm run build

# --- Etapa 2: Servidor Nginx ---
FROM nginx:alpine
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]