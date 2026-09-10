const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

async function procesar(respuesta) {
  if (respuesta.status === 204) return null;
  const cuerpo = await respuesta.json().catch(() => null);
  if (!respuesta.ok) {
    const mensaje = cuerpo?.error ?? `Error HTTP ${respuesta.status}`;
    throw new Error(mensaje);
  }
  return cuerpo;
}

export function crearHttpClient(baseUrl = BASE_URL) {
  return {
    get: (ruta, params = {}) => {
      const url = new URL(baseUrl + ruta);
      Object.entries(params)
        .filter(([, v]) => v !== undefined && v !== null && v !== '')
        .forEach(([k, v]) => url.searchParams.set(k, v));
      return fetch(url).then(procesar);
    },
    post: (ruta, cuerpo) =>
      fetch(baseUrl + ruta, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(cuerpo),
      }).then(procesar),
    delete: (ruta) => fetch(baseUrl + ruta, { method: 'DELETE' }).then(procesar),
  };
}

export const httpClient = crearHttpClient();
