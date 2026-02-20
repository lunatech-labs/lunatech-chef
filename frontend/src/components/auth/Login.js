import React from "react";
import Button from "react-bootstrap/Button";
import Row from 'react-bootstrap/Row';
import { useAuth } from "react-oidc-context";

function Login() {
  const auth = useAuth();

  return (
    <div className="container">
      <Row>
        <h3>Lunatech Chef</h3>
      </Row>
      <Row>
        <p>
          You must be a Lunatech employee with a valid Google account to use
          this software.<br />
        </p>
      </Row>
      <Row>
        <Button
          variant="primary"
          onClick={() => auth.signinRedirect()}
        >
          Sign in with Google
        </Button>
      </Row>
    </div>
  );
}

export default Login;
