import React from "react";
import { useEffect } from "react";
import { useAuth, hasAuthParams } from "react-oidc-context";

// Landing page for the Keycloak redirect. Once the OIDC library finishes the
// code exchange, Main picks the session up and routes back to the app.
function Redirect() {
    const auth = useAuth();

    useEffect(() => {
        if (
            !hasAuthParams() &&
            !auth.isAuthenticated &&
            !auth.activeNavigator &&
            !auth.isLoading
        ) {
            auth.signinRedirect();
        }
    }, [auth]);

    return auth.activeNavigator ? <div>Signing you in/out...</div> : null;
}

export default Redirect;
