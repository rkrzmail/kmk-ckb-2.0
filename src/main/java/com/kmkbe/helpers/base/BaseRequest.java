package com.kmkbe.helpers.base;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;


/**
 * @author hyvercode
 * @date 6/26/26
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseRequest implements Serializable {

}
