import React, { Component } from "react";
import { GoogleLogin } from "react-google-login";

class Login extends Component {
  constructor(props) {
    super(props);

    this.handleLogin = this.handleLogin.bind(this);
    this.handleLoginFailure = this.handleLoginFailure.bind(this);
  }

  handleLogin(response) {
    console.log("tokenId: " + response.tokenId);
    this.props.login(response.tokenId);
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
          this software. <br />
        </p>
        <GoogleLogin
          clientId={process.env.REACT_APP_CLIENT_ID}
          buttonText="Sign in with Google"
          onSuccess={this.handleLogin}
          onFailure={this.handleLoginFailure}
          cookiePolicy={"single_host_origin"}
          responseType="code,token"
        />
      </div>
    );
  }
}

export default Login;
