# Confecciones Villa Acero - App Android Final

App completa y funcional. Para tu proyecto de título.

## Instalación

1. **Cierra Android Studio.**
2. **Borra completamente** la carpeta `C:\Users\Luis\AndroidStudioProjects\appvillaacero1` que tienes ahora (si tiene cosas viejas, las archivas en otro lugar primero por si acaso).
3. **Extrae este zip** en `C:\Users\Luis\AndroidStudioProjects\` de modo que quede la ruta:
   ```
   C:\Users\Luis\AndroidStudioProjects\appvillaacero1\
   ```
4. **Abre Android Studio** → File → Open → seleccioná la carpeta `appvillaacero1`.
5. Android Studio va a hacer **Sync Gradle** automáticamente. Tarda 1-3 minutos la primera vez (descarga dependencias).
6. Cuando termine y veas el botón Run ▶ verde, dale play.

## Si algo falla en el Sync

- Si pide actualizar AGP o Kotlin: dale "Update" y aceptá lo que sugiera.
- Si dice "SDK location not found": File → Project Structure → SDK Location → poné tu Android SDK (usualmente `C:\Users\Luis\AppData\Local\Android\Sdk`).
- Cualquier otro error: pegamelo y lo resolvemos.

## Lo que tiene la app

**4 pantallas + flujo completo end-to-end:**

1. **Home**: lista de 12 colegios, buscador, filtros por comuna (Concepción, Hualpén, Talcahuano, Chiguayante), ícono ⓘ arriba a la derecha para ir a "Sobre nosotros".
2. **Catálogo del colegio**: productos del colegio agrupados por tipo de prenda, filtros por tipo, precio "desde", cantidad de tallas.
3. **Detalle del producto**: imagen placeholder, rango de precios, tabla de tallas con precios. Cada talla es clickeable (abre WhatsApp con mensaje específico). Botón verde grande de WhatsApp abajo (mensaje genérico).
4. **Sobre nosotros**: avatar "VA", contacto WhatsApp + Instagram, zona de atención, lista de colegios clickeables.

**Polish visual:**
- Ícono adaptive azul con "V" blanca.
- Splash screen con la misma identidad.
- Tema Material 3 con paleta Villa Acero.

**Datos:** 12 colegios reales, 639 productos generados con precios y tallas realistas. Cuando tu mamá te pase su catálogo real, editás `app/src/main/res/raw/catalogo.json`.

## Estructura del proyecto

```
app/src/main/java/cl/villaacero/uniformes/
├── MainActivity.kt              # Entry point
├── data/
│   ├── Models.kt                 # Negocio, Colegio, Producto, etc.
│   ├── CatalogRepository.kt      # Carga del JSON, singleton
│   └── CatalogTransforms.kt      # Funciones puras de transformación
├── navigation/
│   ├── Routes.kt                 # Definición de rutas
│   └── AppNavigation.kt          # NavHost con todas las pantallas
├── ui/
│   ├── theme/                    # Color.kt, Theme.kt, Type.kt
│   ├── home/                     # HomeScreen + HomeViewModel
│   ├── catalogo/                 # CatalogoScreen + CatalogoViewModel
│   ├── detalle/                  # DetalleScreen + DetalleViewModel
│   └── nosotros/                 # NosotrosScreen + NosotrosViewModel
└── util/
    └── Format.kt                 # formatPrice, openWhatsApp, openInstagram
```

## Para reemplazar la "V" placeholder por el logo real de tu mamá

Cuando consigas el logo:

1. En Android Studio: File → New → Vector Asset.
2. Seleccioná "Local file (SVG, PSD)" y elegí el logo.
3. Nombrá el resource `ic_launcher_foreground` (sobreescribe el placeholder).
4. Hacé lo mismo para `ic_splash_logo` (tamaño 240×240 dp).
5. Si querés cambiar el color de fondo: editá `app/src/main/res/values/colors.xml`.

## Para actualizar el catálogo cuando tu mamá te pase nuevos datos

Editá `app/src/main/res/raw/catalogo.json`. La estructura es:

```json
{
  "negocio": {
    "nombre": "Confecciones Villa Acero",
    "whatsapp": "56920680021",
    "instagram": "@confecciones.villaacero",
    "instagramUrl": "https://www.instagram.com/confecciones.villaacero/",
    "zonaAtencion": "Gran Concepción"
  },
  "colegios": [
    {
      "id": 1,
      "nombre": "Kingston College",
      "comuna": "Concepción",
      "direccion": "Concepción",
      "productos": [
        { "tipoPrenda": "POLERA M/C", "talla": "8", "precio": 11000, "stock": 5 }
      ]
    }
  ]
}
```

## Versiones de Gradle/Kotlin

- AGP 9.2.1
- Kotlin 2.0.20
- Compose BOM 2024.10.01
- Min SDK 24 (Android 7.0)
- Target SDK 35 (Android 15)

## Para tu defensa de título

Esta app cubre los puntos técnicos siguientes:

- **Arquitectura limpia**: UI / ViewModels / Repository / funciones puras separadas.
- **Material 3 con Jetpack Compose** (stack moderno).
- **Navigation Compose** con type-safe params.
- **Splash Screen API** moderna (compatible API 21+).
- **Adaptive icons** (API 26+) con themed icon support (API 33+).
- **Carga local de JSON** con kotlinx-serialization.
- **Intents externos** (WhatsApp, Instagram) con fallback al navegador.
- **Estado reactivo** con StateFlow + collectAsStateWithLifecycle.

El problema que resuelve: clientes preguntan constantemente en redes sociales si la tienda atiende su colegio y los precios por talla. La app les da esa información directa + un botón que abre WhatsApp con el mensaje ya escrito.

---

Si todo compila y corre, mandame screenshots de las 4 pantallas. Si hay un error en el Sync o en el Run, pegame el error completo.
