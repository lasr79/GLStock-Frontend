# 📱 GLStock – Frontend (Android)

Aplicación Android (Java) que consume la API REST del backend **GLStock-Backend**.  
Permite login con **JWT**, gestión de inventario, usuarios, movimientos y reportes.
--- 
## 🏗️ Arquitectura del proyecto 

![Arquitectura GLStock](https://github.com/user-attachments/assets/6626d44a-3fcf-4239-8cd6-79ee52aaa003)

--- 
## 🏗️ Arquitectura del Frontend


![Arquitectura GLStock](https://github.com/user-attachments/assets/f88e3a26-c30f-4a22-9d27-e181089bdb41)

- UI (Activities & Fragments) → Maneja la interacción con el usuario y muestra la información.
- Adapter (RecyclerViewAdapters) → Conecta la UI con los datos, mostrando listas de productos, usuarios o movimientos.
- API (Retrofit + Interceptor) → Se encarga de la comunicación con el backend (Spring Boot), incluyendo el envío del token JWT en cada petición.
- Model (Clases de datos) → Representa las entidades de negocio (Producto, Usuario, Movimiento, etc.) que viajan entre frontend y backend.
- Util (Helpers y SessionManager) → Proporciona utilidades compartidas, como la gestión segura de la sesión, almacenamiento del token y helpers de autenticación.

--- 
## ✨ Funcionalidades
- 🔐 Autenticación con JWT + roles (ADMIN / GESTOR).
- 📦 Gestión de productos (con imágenes vía Glide).
- 📊 Movimientos de stock (entradas / salidas).
- 📄 Descarga de reportes PDF.
- 🎨 UI moderna con Material Design.

--- 
## 🔧 Configuración
- **Debug (emulador)** → `http://10.0.2.2:8080/`
- **Dispositivo físico en LAN** → `http://192.168.x.x:8080/`
- **Producción (AWS)** → `https://api.tudominio.com/`

--- 
## 📸 Capturas de pantalla


<p align="center">
  <img src="https://github.com/user-attachments/assets/b7a83334-e744-47ee-8c9f-275ae46e9891" alt="Login" width="200"/>
  <img src="https://github.com/user-attachments/assets/3db7476f-c71b-4ca6-aea6-03d7a57eaddb" alt="Dashboard Admin" width="200"/>
  <img src="https://github.com/user-attachments/assets/501a838c-ae51-43fc-8c9c-84461363dff8" alt="Dashboard Gestor" width="200"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/e2494718-0024-40c7-9e1c-0660a8382f19" alt="Productos" width="500"/>
  <img src="https://github.com/user-attachments/assets/e001817c-a94c-4532-9e04-0e2fb988370d" alt="Reportes" width="500"/>
  <img src="https://github.com/user-attachments/assets/1a645a79-2071-4958-b79e-c5a75afa97d2" alt="Usuario" width="500"/>
</p>
--- 
## 🎥 Demo

### Opción A: GIF en el repo
<p align="center">
  <img src="docs/demo.gif" alt="Demo GLStock - Android" width="720"/>
</p>

--- 

## 👤 Autor

Desarrollado por **[Luigi Alessandro Squillaro](https://github.com/lasr79)** 
