import React from "react";
import { Form, Field } from "react-final-form";
import { useNavigate } from "react-router-dom";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

export function AddMenu(props) {
  const navigate = useNavigate();
  const required = (value) => (value ? undefined : "Required");
  const onSubmit = (values) => {
    props.addNewMenu(values);
    navigate("/allmenus");
  };

  function RenderData() {
    return (
      <Form
        onSubmit={onSubmit}
        initialValues={{
          name: "",
          dishesUuids: [],
        }}
        render={({ handleSubmit, submitting, pristine }) => (
          <form onSubmit={handleSubmit}>
            <Row>
              <Col lg="2">Name</Col>
              <Col lg="5">
                <Field validate={required} name="name">
                  {({ input, meta }) => (
                    <div>
                      <input {...input} type="text" placeholder="Name" maxLength={50} />
                      {meta.error && meta.touched && <span>{meta.error}</span>}
                    </div>
                  )}
                </Field>
              </Col>
            </Row>
            <Row> <Col lg="7"> <h6 className="mt-4">Add dishes to the menu:</h6></Col></Row>
            {props.dishes.map((dish) => {
              return (
                <Row key={dish.uuid}>
                  <Col lg="2">{dish.name}</Col>
                  <Col lg="1">
                    <Field
                      name="dishesUuids"
                      component="input"
                      type="checkbox"
                      value={dish.uuid}
                    ></Field>
                  </Col>
                </Row>
              );
            })}
            <Row className="mt-4">
              <Col lg="5">
                <div className="d-grid">
                  <Button
                    type="submit"
                    variant="success"
                    disabled={submitting || pristine}
                  >
                    Add Menu
                  </Button>
                </div>
              </Col>
            </Row>
          </form>
        )}
      ></Form>
    );
  }

  return (
    <div className="container">
      <div>
        <h3 className="mt-4 mb-4">New Menu</h3>
      </div>
      <RenderData />
    </div>
  );
};
