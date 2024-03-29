import React from "react";
import { Form, Field } from "react-final-form";
import { useNavigate } from "react-router-dom";
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

export function AddDish(props) {
  const navigate = useNavigate();
  const required = (value) => (value ? undefined : "Required");
  const onSubmit = (values) => {
    props.addNewDish(values);
    navigate("/alldishes");
  };

  function RenderData() {
    return (
      <Form
        onSubmit={onSubmit}
        initialValues={{
          name: "",
          description: "",
          isVegetarian: false,
          isHalal: false,
          hasNuts: false,
          hasSeafood: false,
          hasPork: false,
          hasBeef: false,
          isGlutenFree: false,
          isLactoseFree: false,
        }}
        render={({ handleSubmit, submitting, pristine }) => (
          <form onSubmit={handleSubmit}>
            <Row>
              <Col lg="1">Name</Col>
              <Col lg="5">
                <Field validate={required} name="name">
                  {({ input, meta }) => (
                    <div className="d-grid">
                      <input {...input} placeholder="Name" maxLength={60} />
                      {meta.error && meta.touched && <span className="text-danger">  {meta.error}</span>}
                    </div>
                  )}
                </Field>
              </Col>
            </Row>
            <Row>
              <Col lg="1">Details</Col>
              <Col lg="5">
                <Field name="description">
                  {({ input, meta }) => (
                    <div className="d-grid">
                      <textarea {...input} placeholder="Description just for admins" maxLength={200} size="2" />
                      {meta.error && meta.touched && <span className="text-danger">  {meta.error}</span>}
                    </div>
                  )}
                </Field>
              </Col>
            </Row>
            <Row className="mt-4">
              <Col lg="4">Choose all that apply:</Col>
            </Row>
            <Row>
              <Col lg="4">
                <Field name="isVegetarian" component="input" type="checkbox" ></Field><span> Is vegetarian</span>
              </Col>
            </Row>
            <Row>
              <Col lg="4">
                <Field name="isHalal" component="input" type="checkbox"></Field> <span> Is halal</span>
              </Col>
            </Row>
            <Row>
              <Col lg="4">
                <Field name="hasNuts" component="input" type="checkbox"></Field><span> Contains nuts</span>
              </Col>
            </Row>
            <Row>
              <Col lg="4">
                <Field name="hasSeafood" component="input" type="checkbox"></Field><span> Contains seafood</span>
              </Col>
            </Row>
            <Row>
              <Col lg="4">
                <Field name="hasPork" component="input" type="checkbox"></Field><span> Contains pork</span>
              </Col>
            </Row>
            <Row>
              <Col lg="4">
                <Field name="hasBeef" component="input" type="checkbox"></Field><span> Contains beef</span>
              </Col>
            </Row>
            <Row>
              <Col lg="4">
                <Field name="isGlutenFree" component="input" type="checkbox"  ></Field><span> Is gluten free</span>
              </Col>
            </Row>
            <Row>
              <Col lg="4">
                <Field name="isLactoseFree" component="input" type="checkbox" ></Field><span> Is lactose free</span>
              </Col>
            </Row>
            <Row className="mt-4">
              <Col lg="6">
                <div className="d-grid">
                  <Button
                    type="submit"
                    variant="success"
                    disabled={submitting || pristine}
                  >
                    Add Dish
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
        <h3 className="mt-4 mb-4">New Dish</h3>
      </div>
      <RenderData />
    </div>
  );
};
