package pl.marcinm312.springbootspotify.model.security;

public record TestTokenData(String access_token, String token_type, Integer expires_in) {

}
