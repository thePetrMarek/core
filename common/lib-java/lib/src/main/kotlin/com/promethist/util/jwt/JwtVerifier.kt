package com.promethist.util.jwt

import com.auth0.jwt.interfaces.DecodedJWT

interface JwtVerifier {
    fun verify(jwt: DecodedJWT): DecodedJWT
}