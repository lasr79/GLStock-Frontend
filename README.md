# 📱 GLStock – Frontend (Android)

Aplicación Android (Java) que consume la API REST del **GLStock-Backend**.  
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
  <img src="https://github.com/user-attachments/assets/5cff7661-b422-4fb5-9663-933a9973ecc1" alt="Login" width="300"/>
  <img src="https://github.com/user-attachments/assets/ce0e23a8-9503-42bb-b211-0a9f5906a94a" alt="Dashboard Admin" width="300"/>
  <img src="https://github.com/user-attachments/assets/8c9d24a0-7593-4f53-aba6-2e5496ab05cf" alt="Dashboard Gestor" width="300"/>
</p>


<p align="center">
  <img src="https://github.com/user-attachments/assets/7229486e-2b00-49d3-b1b5-67c939d595de" alt="Productos" width="300"/>
  <img src="https://github.com/user-attachments/assets/e0c4081f-e1c3-4baf-9f13-ab3c283bf326" alt="Reportes" width="300"/>
  <img src="https://github.com/user-attachments/assets/d8f723c7-a93b-44bb-8905-585f6fd5a1c0" alt="Usuario" width="300"/>
</p>
--- 

## 🎥 Demo

            [Ver / descargar la demo (MP4)](assets/demo.mp4?raw=1)

--- 

## 👤 Autor

Desarrollado por **[Luigi Alessandro Squillaro](https://github.com/lasr79)** 
