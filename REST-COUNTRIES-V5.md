# Monedas por pais con REST Countries v5

La consulta GET /currency/exchange-rate/{name} utiliza REST Countries v5.
Se conserva el formato currency.currencies (mapa por codigo) y currency.flags
(png, svg, alt) que esperaba el frontend.

En IntelliJ, edita la configuracion de ejecucion CurrencyServiceApplication:
Modify options > Environment variables.
Agrega RESTCOUNTRIES_API_KEY con tu clave propia de REST Countries.
Puedes usar la misma clave configurada en country-service, si sus permisos lo permiten.
No guardes la clave en archivos versionados.

Base predeterminada: https://api.restcountries.com/countries/v5
RESTCOUNTRIES_BASE_URL permite reemplazarla.
Autenticacion: Authorization: Bearer, enviada solo desde el backend.
Documentacion: https://restcountries.com/docs/countries

Con Config y Discovery disponibles, reinicia CurrencyServiceApplication.
Si Config mantiene el puerto 8085, consulta:
http://localhost:8085/currency/exchange-rate/peru

La consulta devuelve el primer pais coincidente, como antes.
Errores: 400 para nombre vacio, 404 sin resultados, 503 para clave ausente,
rechazada, demo o cuota agotada, y 502 para fallos del proveedor.
La API ExchangeRate-API de conversion y listado de monedas sigue igual.

Verificacion: .\mvnw.cmd test con JDK 25.
Las pruebas usan respuestas simuladas; no necesitan clave ni consumen cuota.
