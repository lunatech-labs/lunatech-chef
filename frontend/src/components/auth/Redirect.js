import React from "react";
import { useEffect } from "react";
import { useAuth, hasAuthParams } from "react-oidc-context";
import { useNavigate } from "react-router-dom";
import { User } from "oidc-client-ts";

export function Redirect(props) {
    const auth = useAuth();
    const navigate = useNavigate();

    const handleLogin = (token) => {
        // console.log("tokenId: " + token);
        props.login(token);
    }

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
            // get the authentication token from session storage if it exists
            const oidcStorage =
                sessionStorage.getItem(
                    `oidc.user:${process.env.REACT_APP_REALMS_URL}:${process.env.REACT_APP_CLIENT_ID}`
                ) ?? "";

            const { id_token } = User.fromStorageString(oidcStorage);

            handleLogin(id_token);
            navigate("/");
        }
    }, [auth.isAuthenticated, auth.activeNavigator, auth.isLoading]);

    function ActiveNavigator() {
        return (
            (auth.activeNavigator) ? (
                <div>Signing you in/out...</div>
            ) : <div></div>
        );
    }


    return (<ActiveNavigator />);

}

export default Redirect;
