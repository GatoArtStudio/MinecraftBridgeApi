# Minecraft Bridge API

API Java independiente de loaders para conectar mods, plugins y otras aplicaciones relacionadas con Minecraft mediante un contrato común.

La librería no depende de Fabric, Forge, NeoForge, Bukkit, Spigot, Paper ni de ningún otro mod loader o plugin loader. Cada mod, plugin o aplicación puede implementar la interfaz `Bridge` y registrarla para que otros componentes puedan utilizarla.

## Requisitos

- Java 17 o superior.
- Maven o Gradle para instalar la dependencia.
- Para publicar versiones nuevas mediante JitPack, el repositorio debe ser público en GitHub.

La API utiliza las anotaciones `@NotNull` y `@Nullable` de JetBrains para documentar valores obligatorios y opcionales. La dependencia se incluye transitivamente al instalar la librería.

## Instalación

La librería se distribuye mediante [JitPack](https://jitpack.io/).

### Maven

Añade el repositorio:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Añade la dependencia:

```xml
<dependency>
    <groupId>com.github.GatoArtStudio</groupId>
    <artifactId>MinecraftBridgeApi</artifactId>
    <version>v3.0.0</version>
</dependency>
```

### Gradle

En `build.gradle`:

```gradle
repositories {
    maven {
        name = "JitPack"
        url = "https://jitpack.io"
    }
}

dependencies {
    implementation("com.github.GatoArtStudio:MinecraftBridgeApi:v3.0.0")
}
```

### Ejemplo: Fabric Loom

Para un mod de Fabric, la librería debe quedar incluida dentro del JAR final del mod:

```gradle
repositories {
    maven {
        name = "JitPack"
        url = "https://jitpack.io"
    }
}

dependencies {
    implementation("com.github.GatoArtStudio:MinecraftBridgeApi:v3.0.0")
    include(implementation("com.github.GatoArtStudio:MinecraftBridgeApi:v3.0.0"))
}
```

Después genera el mod con:

```bash
./gradlew clean build --refresh-dependencies
```

La dependencia es una librería Java normal, no un mod de Fabric. Por eso se utiliza `implementation` y no `modImplementation`. En otros loaders o sistemas de plugins, utiliza la configuración equivalente para incluir una librería Java normal en el artefacto final.

## Compatibilidad

La API puede utilizarse desde:

- Mods de Fabric, Forge o NeoForge.
- Plugins de Bukkit, Spigot, Paper u otros sistemas compatibles.
- Aplicaciones Java externas.
- Cualquier proyecto Java que pueda consumir una dependencia Maven o Gradle.

El proyecto consumidor es responsable de incluir `MinecraftBridgeApi` en su classpath durante la ejecución. La API no intenta detectar ni cargar automáticamente ningún loader.

## API principal

Los tipos públicos se encuentran en el paquete:

```text
com.gatoartstudio.api
```

### Implementar `Bridge`

Una implementación debe definir las operaciones del bridge:

```java
import com.gatoartstudio.api.Bridge;
import com.gatoartstudio.api.BridgeRequest;
import com.gatoartstudio.api.BridgeResponse;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MyBridge implements Bridge {
    @Override
    public CompletableFuture<String> ping() {
        return CompletableFuture.completedFuture("pong");
    }

    @Override
    public CompletableFuture<String> getPlayerName(UUID playerId) {
        String playerName = "Steve";
        return CompletableFuture.completedFuture(playerName);
    }

    @Override
    public CompletableFuture<BridgeResponse> requestInformation(BridgeRequest request) {
        if (request.type().equals("player")) {
            return CompletableFuture.completedFuture(
                    BridgeResponse.success("Información del jugador: " + request.payload())
            );
        }

        return CompletableFuture.completedFuture(
                BridgeResponse.failure("Tipo de solicitud no soportado: " + request.type())
        );
    }
}
```

### Registrar el bridge

Registra una sola implementación durante la inicialización del mod o plugin:

```java
import com.gatoartstudio.api.BridgeRegistry;

BridgeRegistry.register(new MyBridge());
```

Puedes comprobar si existe una implementación registrada:

```java
if (BridgeRegistry.isAvailable()) {
    BridgeRegistry.get().ping().thenAccept(System.out::println);
}
```

Si no existe un bridge registrado, `BridgeRegistry.get()` lanza `IllegalStateException`.

### Solicitar información

`BridgeRequest` contiene el tipo de solicitud y su contenido:

```java
import com.gatoartstudio.api.BridgeRequest;
import com.gatoartstudio.api.BridgeResponse;

BridgeRequest request = new BridgeRequest("player", "uuid-del-jugador");

BridgeRegistry.get()
        .requestInformation(request)
        .thenAccept(response -> {
            if (response.success()) {
                System.out.println(response.response());
            } else {
                System.err.println(response.errorMessage());
            }
        });
```

### Crear respuestas

Para una operación exitosa:

```java
BridgeResponse.success("respuesta");
```

Para una operación fallida:

```java
BridgeResponse.failure("No se encontró el jugador");
```

Una respuesta exitosa no puede tener mensaje de error y una respuesta fallida debe tenerlo.

## Eventos genéricos

El sistema de eventos no depende de clases concretas del mod o plugin receptor. Los eventos se identifican mediante un `type`, transportan un `payload` en formato `String` y contienen un timestamp en milisegundos.

Registrar un bus global:

```java
import com.gatoartstudio.api.DefaultEventBus;
import com.gatoartstudio.api.EventBusRegistry;

EventBusRegistry.register(new DefaultEventBus());
```

Suscribirse a un tipo de evento:

```java
import com.gatoartstudio.api.EventBusRegistry;

var subscription = EventBusRegistry.get().subscribe(
        "minecraft.player.joined",
        event -> System.out.println(event.payload())
);
```

Emitir un evento:

```java
import com.gatoartstudio.api.BridgeEvent;
import com.gatoartstudio.api.EventBusRegistry;

EventBusRegistry.get().emit(
        BridgeEvent.now(
                "minecraft.player.joined",
                "{\"playerId\":\"uuid\",\"name\":\"Steve\"}"
        )
);
```

Cancelar una suscripción:

```java
subscription.unsubscribe();
```

Los listeners se ejecutan de forma síncrona. Si un listener lanza una excepción, los demás listeners continúan ejecutándose. Los errores pueden personalizarse mediante `EventErrorHandler` al crear `DefaultEventBus`.

Las excepciones de validación y de registro se indican en las firmas mediante `throws`. Las operaciones que devuelven `CompletableFuture` pueden completar el future excepcionalmente cuando ocurre un error asíncrono.

## Pruebas

Ejecuta las pruebas con:

```bash
mvn clean verify
```

## Versiones

La versión estable actual es `v3.0.0`.

Las versiones se publican mediante tags de Git y son construidas por JitPack. Para utilizar otra versión, reemplaza `v3.0.0` por el tag correspondiente.

Puedes consultar todas las versiones disponibles y el estado de sus compilaciones en [JitPack](https://jitpack.io/#GatoArtStudio/MinecraftBridgeApi).

## Licencia

Este proyecto todavía no define una licencia pública. Añade un archivo `LICENSE` antes de distribuirlo formalmente.
