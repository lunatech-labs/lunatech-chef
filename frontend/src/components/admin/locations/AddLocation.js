import React, { Component } from "react";

import { Button, FormLabel, Row } from "react-bootstrap";
import { Control, Form, Errors } from "react-redux-form";

const required = (val) => val && val.length;

class AddLocation extends Component {
  constructor(props) {
    super(props);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleSubmit(values) {
    this.props.addNewLocation(values);
    this.props.resetNewLocationForm();
  }

  render() {
    return (
      <div className="container">
        <div className="row row-content">
          <div className="col-12">
            <h3>New Location</h3>
          </div>
          <Form
            model="newLocation"
            onSubmit={(values) => this.handleSubmit(values)}
          >
            <Row className="form-group row">
              <FormLabel className="col-sm-4">City</FormLabel>
              <Control.text
                className="form-control col-sm-8"
                model=".city"
                id="city"
                name="city"
                placeholder="City"
                validators={{
                  required,
                }}
              />
              <Errors
                className="text-danger"
                model=".firstname"
                show="touched"
                messages={{
                  required: "Required.",
                }}
              />
            </Row>
            <Row className="form-group row">
              <FormLabel className="col-sm-4">Country</FormLabel>
              <Control.text
                className="form-control col-sm-8"
                model=".country"
                id="country"
                name="country"
                placeholder="Country"
                validators={{
                  required,
                }}
              />
              <Errors
                className="text-danger"
                model=".firstname"
                show="touched"
                messages={{
                  required: "Required.",
                }}
              />
            </Row>
            <Button type="submit" variant="primary">
              Add Location
            </Button>
          </Form>
        </div>
      </div>
    );
  }
}

export default AddLocation;
