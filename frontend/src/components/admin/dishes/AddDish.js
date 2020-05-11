import React, { Component } from "react";

import { Button, FormLabel, Row } from "react-bootstrap";
import { Control, Form, Errors } from "react-redux-form";

const required = (val) => val && val.length;

class AddDish extends Component {
  constructor(props) {
    super(props);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleSubmit(values) {
    this.props.addNewDish(values);
    this.props.resetNewDishForm();
  }

  render() {
    return (
      <div className="container">
        <div className="row row-content">
          <div className="col-12">
            <h3>New Dish</h3>
          </div>
          <Form
            model="newDish"
            onSubmit={(values) => this.handleSubmit(values)}
          >
            <Row className="form-group row">
              <FormLabel className="col-sm-5">Name</FormLabel>
              <Control.text
                className="form-control col-sm-7"
                model=".name"
                id="name"
                name="name"
                placeholder="Name"
                validators={{
                  required,
                }}
              />
              <Errors
                className="text-danger"
                model=".name"
                show="touched"
                messages={{
                  required: "Required.",
                }}
              />
            </Row>
            <Row className="form-group row">
              <FormLabel className="col-sm-5">Description</FormLabel>
              <Control.text
                className="form-control col-sm-7"
                model=".description"
                id="description"
                name="description"
                placeholder="Description"
                width="100px"
              />
            </Row>
            <Row className="form-group row">
              <FormLabel className="col-sm-5">Vegetarian</FormLabel>
              <Control.checkbox
                className="form-control col-sm-7"
                model=".vegetarian"
                id="vegetarian"
                name="vegetarian"
              />
            </Row>
            <Row className="form-group row">
              <FormLabel className="col-sm-5">Nuts</FormLabel>
              <Control.checkbox
                className="form-control col-sm-7"
                model=".hasNuts"
                id="hasNuts"
                name="hasNuts"
              />
            </Row>
            <Row className="form-group row">
              <FormLabel className="col-sm-5">Seafood</FormLabel>
              <Control.checkbox
                className="form-control col-sm-7"
                model=".hasSeafood"
                id="hasSeafood"
                name="hasSeafood"
              />
            </Row>
            <Row className="form-group row">
              <FormLabel className="col-sm-5">Pork</FormLabel>
              <Control.checkbox
                className="form-control col-sm-7"
                model=".hasPork"
                id="hasPork"
                name="hasPork"
              />
            </Row>
            <Row className="form-group row">
              <FormLabel className="col-sm-5">Beef</FormLabel>
              <Control.checkbox
                className="form-control col-sm-7"
                model=".hasBeef"
                id="hasBeef"
                name="hasBeef"
              />
            </Row>
            <Row className="form-group row">
              <FormLabel className="col-sm-5">GlutenFree</FormLabel>
              <Control.checkbox
                className="form-control col-sm-7"
                model=".isGlutenFree"
                id="isGlutenFree"
                name="isGlutenFree"
              />
            </Row>
            <Row className="form-group row">
              <FormLabel className="col-sm-5">Lactose</FormLabel>
              <Control.checkbox
                className="form-control col-sm-7"
                model=".hasLactose"
                id="hasLactose"
                name="hasLactose"
              />
            </Row>
            <Button type="submit" variant="primary">
              Add Dish
            </Button>
          </Form>
        </div>
      </div>
    );
  }
}

export default AddDish;
