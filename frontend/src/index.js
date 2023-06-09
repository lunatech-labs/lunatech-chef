import App from "./App";
import React from "react";
import { createRoot } from 'react-dom/client';
import { ConfigureStore } from "./redux/ConfigureStore";
import { BrowserRouter } from "react-router-dom";
import { Provider } from "react-redux";
import { AuthProvider } from "react-oidc-context";
import "bootstrap/dist/css/bootstrap.min.css";
import "react-datepicker/dist/react-datepicker.css";
import "./css/index.css";
import * as serviceWorker from "./serviceWorker";

const store = ConfigureStore();
const container = document.getElementById('root');
const root = createRoot(container);

const oidcConfig = {
  authority: `${process.env.REACT_APP_REALMS_URL}`,
  client_id: `${process.env.REACT_APP_CLIENT_ID}`,
  redirect_uri: `${window.location.origin}/redirect`,
};

root.render(
  <AuthProvider {...oidcConfig}>
    <Provider store={store}>
      <React.StrictMode>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </React.StrictMode>
    </Provider>
  </AuthProvider>
);


// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
