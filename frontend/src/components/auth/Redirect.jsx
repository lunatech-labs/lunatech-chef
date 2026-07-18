import React from "react";
import { useEffect } from "react";
import { useAuth, hasAuthParams } from "react-oidc-context";
import { useNavigate } from "react-router-dom";

function Redirect({ login }) {
    const auth = useAuth();
    const navigate = useNavigate();

    useEffect(() => {
        if (
            !hasAuthParams() &&
            !auth.isAuthenticated &&
            !auth.activeNavigator &&
            !auth.isLoading
        ) {
            auth.signinRedirect();
        }

        if (auth.isAuthenticated && !auth.activeNavigator && !auth.isLoading) {
            login(auth.user.id_token);
            navigate("/");
        }
    }, [auth, navigate, login]);

    return auth.activeNavigator ? <div>Signing you in/out...</div> : null;
}

export default Redirect;
