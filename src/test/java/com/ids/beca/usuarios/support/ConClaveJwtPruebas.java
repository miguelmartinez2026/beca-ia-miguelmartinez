package com.ids.beca.usuarios.support;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.ContextConfiguration;

/**
 * Proporciona la clave JWT generada en memoria a las pruebas que cargan el contexto Spring completo.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@ContextConfiguration(initializers = ClaveJwtPruebas.Inicializador.class)
public @interface ConClaveJwtPruebas {
}
