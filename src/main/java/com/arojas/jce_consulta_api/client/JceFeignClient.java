/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.arojas.jce_consulta_api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author arojas
 *         * Cliente Feign para comunicarse con el servicio JCE
 *
 */

@FeignClient(name = "jce-client", url = "${app.jce.base-url}", configuration = JceFeignClientConfiguration.class)
public interface JceFeignClient {

	/**
	 * Consulta datos de cédula en el servicio JCE
	 *
	 * @param serviceId Service ID del JCE
	 * @param id1       Código de municipio (3 dígitos)
	 * @param id2       Secuencia de cédula (7 dígitos)
	 * @param id3       Dígito verificador (1 dígito)
	 * @return Respuesta XML del servicio JCE
	 */
	@GetMapping("${app.jce.endpoint}")
	String consultarCedula(
			@RequestParam("ServiceID") String serviceId,
			@RequestParam("ID1") String id1,
			@RequestParam("ID2") String id2,
			@RequestParam("ID3") String id3);
}
