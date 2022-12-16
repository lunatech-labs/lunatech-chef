import React from "react";
import { Form, Field } from "react-final-form";
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

export const UserProfile = (props) => {
  const onSubmit = (values) => {
    props.saveUserProfile(props.user.uuid, values);
  };

  function RenderData() {
    return (
      <Row>
        <Form
          onSubmit={onSubmit}
          initialValues={{
            locationUuid: props.user.locationUuid,
            isVegetarian: props.user.isVegetarian,
            hasHalalRestriction: props.user.hasHalalRestriction,
            hasNutsRestriction: props.user.hasNutsRestriction,
            hasSeafoodRestriction: props.user.hasSeafoodRestriction,
            hasPorkRestriction: props.user.hasPorkRestriction,
            hasBeefRestriction: props.user.hasBeefRestriction,
            isGlutenIntolerant: props.user.isGlutenIntolerant,
            isLactoseIntolerant: props.user.isLactoseIntolerant,
            otherRestrictions: props.user.otherRestrictions,
          }}
          render={({ handleSubmit, submitting, pristine }) => (
            <form onSubmit={handleSubmit}>
              <Row>
                {" "}
                <Col lg="2">Name: </Col> <Col lg="3">{props.user.name}</Col>
              </Row>
              <Row>
                {" "}
                <Col lg="2">E-mail: </Col> <Col lg="3">{props.user.emailAddress}</Col>
              </Row>
              <Row>
                <Col lg="2">Prefered location: *</Col>
                <Col lg="3">
                  <div className="select">
                    <Field name="locationUuid" component="select">
                      <option value="" />
                      {props.locations.map((location, index, arr) => {
                        return (
                          <option value={location.uuid} key={location.uuid}>
                            {location.city}, {location.country}
                          </option>
                        );
                      })}
                    </Field>
                  </div>
                </Col>
              </Row>
              <Row></Row>
              <Row>
                <Col className="fw-bold">
                  ** Please use this space for real diet restrictions and not diet preferences **
                </Col>
              </Row>
              <Row>
                <Col lg="2">
                  <Field name="isVegetarian" component="input" type="checkbox"></Field>
                </Col>
                <Col lg="3">I'm vegetarian</Col>
              </Row>
              <Row>
                <Col lg="2">
                  <Field name="isGlutenIntolerant" component="input" type="checkbox"></Field>
                </Col>
                <Col>I'm gluten intolerant</Col>
              </Row>
              <Row>
                <Col lg="2">
                  <Field name="isLactoseIntolerant" component="input" type="checkbox" ></Field>
                </Col>
                <Col lg="3">I'm lactose intolerant</Col>
              </Row>
              <Row>
                <Col lg="2">
                  <Field name="hasHalalRestriction" component="input" type="checkbox"></Field>
                </Col>
                <Col lg="3">I only eat halal food</Col>
              </Row>
              <Row>
                <Col lg="2">
                  <Field name="hasNutsRestriction" component="input" type="checkbox"></Field>
                </Col>
                <Col lg="3">I have a nuts allergy</Col>
              </Row>
              <Row>
                <Col lg="2">
                  <Field name="hasSeafoodRestriction" component="input" type="checkbox"></Field>
                </Col>
                <Col lg="3">I have seafood allergy</Col>
              </Row>
              <Row>
                <Col lg="2">
                  <Field name="hasPorkRestriction" component="input" type="checkbox"></Field>
                </Col>
                <Col lg="3">I don't eat pork</Col>
              </Row>
              <Row>
                <Col lg="2">
                  <Field name="hasBeefRestriction" component="input" type="checkbox"></Field>
                </Col>
                <Col lg="3">I don't eat beef</Col>
              </Row>
              <Row>
                <Col lg="2">Other restrictions:</Col>
                <Col lg="3">
                  <div className="d-grid">
                    <Field name="otherRestrictions" component="textarea" lines="2" ></Field>
                  </div>
                </Col>
              </Row>
              <Row>
                <Col lg="5">
                  <div className="d-grid">
                    <Button
                      type="submit"
                      variant="primary"
                      disabled={submitting || pristine}
                    >
                      Save
                    </Button>
                  </div>
                </Col>
              </Row>
            </form>
          )}
        ></Form>
      </Row>
    );
  }

  return (
    <Container>
      <Row>
        <h3 className="mt-4">User Profile</h3>
      </Row>
      <RenderData />
    </Container>
  );
};
