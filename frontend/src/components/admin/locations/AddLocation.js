import React, { Component } from "react";

import { Button, FormLabel, FormGroup } from "react-bootstrap";
import { Control, Form, Errors } from "react-redux-form";

const required = (val) => val && val.length;

class AddLocation extends Component {
  constructor(props) {
    super(props);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleSubmit(values) {
    this.props.addNewlocation(values);
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
            <FormGroup controlId="formCity">
              <FormLabel md={2}>City</FormLabel>
              <Control.text
                model=".city"
                id="city"
                name="city"
                placeholder="City"
                className="form-control"
                validators={{
                  required,
                }}
              />
              <Errors
                className="text-danger"
                model=".firstname"
                show="touched"
                messages={{
                  required: "Required. ",
                }}
              />
            </FormGroup>
            <FormGroup controlId="formCountry">
              <FormLabel md={2}>Country</FormLabel>
              <Control.text
                model=".country"
                id="country"
                name="country"
                placeholder="Country"
                className="form-control"
                validators={{
                  required,
                }}
              />
              <Errors
                className="text-danger"
                model=".firstname"
                show="touched"
                messages={{
                  required: "Required. ",
                }}
              />
            </FormGroup>
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
