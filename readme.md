
1. ejecutar el comando (teniendo la app docker desktop inicializada) 
```console
docker compose up -d
```
2. ejecutar para levantar los microservicios y sus bases de datos

``` 
iniciar-todo.bat
 ```

3. url peticiones http:
``` 
http://localhost:8081/usuarios
http://localhost:8082/roles
http://localhost:8083/items
http://localhost:8084/carritos
http://localhost:8085/categorias
```
4. formato para los post
``` 
    "nombre" : "admin",
    "descripcion" : "rol con permisos de admin"
}
``` 
5. ver en dbeaver las bases de datos y tablas 


