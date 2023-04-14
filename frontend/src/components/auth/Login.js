import React, { Component } from "react";
import { GoogleLogin } from '@react-oauth/google';

class Login extends Component {
  constructor() {
    super();

    this.handleLogin = this.handleLogin.bind(this);
    this.handleLoginFailure = this.handleLoginFailure.bind(this);
  }

  handleLogin(response) {
    // console.log("tokenId: " + response.credential);
    this.props.login(response.credential);
  }

  handleLoginFailure(response) {
    console.log("Failed to login " + JSON.stringify(response));
  }

  render() {
    return (
      <div className="container">
        <h2>Lunatech Chef</h2>
        <p>
          You must be a Lunatech employee with a valid Google account to use
          this software.<br />
        </p>
        <GoogleLogin
          onSuccess={this.handleLogin}
          onError={this.handleLoginFailure}
          useOneTap
        />
      </div>
    );
  }
}

export default Login;
