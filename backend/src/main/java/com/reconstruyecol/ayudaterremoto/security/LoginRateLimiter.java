package com.reconstruyecol.ayudaterremoto.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Rate limiting simple en memoria para /api/auth/login (no hay proteccion contra fuerza bruta
 * de otra forma). Suficiente para una sola instancia como la del MVP; si en el futuro se corren
 * varias instancias, esto habria que moverlo a un almacen compartido (Redis, etc.).
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_INTENTOS = 5;
    private static final Duration VENTANA = Duration.ofMinutes(15);

    private final Map<String, List<Instant>> intentosPorClave = new ConcurrentHashMap<>();

    public void verificarPermitido(String clave) {
        List<Instant> vigentes = intentosVigentes(clave);
        if (vigentes.size() >= MAX_INTENTOS) {
            throw new DemasiadosIntentosException(
                    "Demasiados intentos fallidos. Espera unos minutos antes de volver a intentar.");
        }
    }

    public void registrarFallo(String clave) {
        intentosPorClave.computeIfAbsent(clave, k -> new CopyOnWriteArrayList<>()).add(Instant.now());
    }

    public void registrarExito(String clave) {
        intentosPorClave.remove(clave);
    }

    private List<Instant> intentosVigentes(String clave) {
        List<Instant> intentos = intentosPorClave.get(clave);
        if (intentos == null) {
            return List.of();
        }
        Instant limite = Instant.now().minus(VENTANA);
        return intentos.stream().filter(i -> i.isAfter(limite)).collect(Collectors.toList());
    }
}
