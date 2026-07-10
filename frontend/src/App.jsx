import React from "react";
import "./css/App.css";
import Main from "./components/Main";
import Container from 'react-bootstrap/Container';

function App() {
  return (
    <Container className="App" >
      <Main />
    </Container>
  );
}

export default App;