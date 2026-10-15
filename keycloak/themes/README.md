# Tema de login de Keycloak (Mathtexpedia)

Tema de las pantallas de Keycloak (iniciar sesión, registro, recuperar contraseña, etc.) con el
mismo aspecto que `/auth/login` del frontend: tokens de `app/globals.css` del frontend (shadcn, zinc), Geist,
imagen lateral y modo oscuro según el sistema (`prefers-color-scheme`).

Hereda del tema `keycloak` de Keycloak 26 y **solo añade estilos y textos**: no sobrescribe
plantillas, así que el flujo (Google, "seguir conectado", verificación de email, validaciones…)
es el de siempre.

```
keycloak/themes/mathtexpedia/login/
├── theme.properties            # parent=keycloak + estilos propios
├── messages/                   # textos (es/en) que sustituyen a los del tema base
└── resources/{css,fonts,img}   # mathtexpedia.css, Geist (SIL OFL 1.1) y logo
```

## Activación

Ya está cableado en este repo:

- `docker-compose.yml` monta `./keycloak/themes/mathtexpedia` en
  `/opt/keycloak/themes/mathtexpedia` (solo lectura).
- `keycloak/realm-export.json` fija `"loginTheme": "mathtexpedia"`. Solo se aplica en el import
  inicial: en un realm ya creado hay que cambiarlo a mano (consola de administración →
  *Realm settings* → *Themes* → *Login theme*).

En `start-dev` los cambios de CSS/mensajes se ven al recargar. En producción (`start`) Keycloak
cachea los temas: reiniciar tras cada cambio.

## Notas

- El nombre "MathTexpedia" de la cabecera se pinta con CSS (`#kc-header-wrapper::after`) porque
  el realm no tiene *Display name* y Keycloak mostraría `mathtexpedia` en minúsculas. Si se
  rellena el display name del realm, se puede quitar ese bloque.
- El logo lateral (`resources/img/logo.png`) es copia de `public/logo.png` del frontend; si cambia el diseño o el logo allí, actualizar aquí
  los tokens `--mx-*` de `mathtexpedia.css` y la imagen.
- No cubre la consola de cuenta ni los emails de Keycloak (otros tipos de tema).
