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

> ⚠️ En release, desactivar logs y usar solo HTTPS.

--- 
## 📸 Capturas de pantalla
<p align="center">
  <img src="https://github.com/user-attachments/assets/82dcdea8-2260-40ad-b670-6fd6713ca459" alt="Login" width="150"/>
  <img src="https://github.com/user-attachments/assets/741b42e0-5fc7-4d59-a031-4d6a130f7930)" alt="Dashboard Admin" width="150"/>
  <img src="https://github.com/user-attachments/assets/xxxxx3" alt="Reportes" width="150"/>
  <img src="https://github.com/user-attachments/assets/xxxxx4" alt="Usuarios" width="150"/>
  <img src="https://github.com/user-attachments/assets/xxxxx5" alt="Movimientos" width="150"/>
  <img src="https://github.com/user-attachments/assets/xxxxx6" alt="Detalle" width="150"/>
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
