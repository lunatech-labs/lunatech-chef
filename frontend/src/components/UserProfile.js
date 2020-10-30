import React from "react";
import { Form, Field } from "react-final-form";

export const UserProfile = (props) => {
  const onSubmit = (values) => {
    props.saveUserProfile(props.user.uuid, values);
  };

  function RenderData() {
    return (
      <Form
        onSubmit={onSubmit}
        initialValues={{
          locationUuid: props.user.locationUuid,
          isVegetarian: props.user.isVegetarian,
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
            <div>
              {" "}
              <label>Name: </label> <label>{props.user.name}</label>
            </div>
            <div>
              {" "}
              <label>E-mail: </label> <label>{props.user.emailAddress}</label>
            </div>
            <label>Choose the location:</label>
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
            <div></div>
            <div>
              <label>Diet restrictions</label>
            </div>
            <div>
              <label>
                ** Please use this space for real diet restrictions and not diet
                preferences **
              </label>
            </div>
            <div>
              <Field
                name="isVegetarian"
                component="input"
                type="checkbox"
              ></Field>
              <label>Vegetarian</label>
            </div>
            <div>
              <Field
                name="hasNutsRestriction"
                component="input"
                type="checkbox"
              ></Field>
              <label>I have a nuts allergy</label>
            </div>
            <div>
              <Field
                name="hasSeafoodRestriction"
                component="input"
                type="checkbox"
              ></Field>
              <label>I have seafood allergy</label>
            </div>
            <div>
              <Field
                name="hasPorkRestriction"
                component="input"
                type="checkbox"
              ></Field>
              <label>I don't eat pork</label>
            </div>
            <div>
              <Field
                name="hasBeefRestriction"
                component="input"
                type="checkbox"
              ></Field>
              <label>I don't eat beef</label>
            </div>
            <div>
              <Field
                name="isGlutenIntolerant"
                component="input"
                type="checkbox"
              ></Field>
              <label>I am gluten intolerant</label>
            </div>
            <div>
              <Field
                name="isLactoseIntolerant"
                component="input"
                type="checkbox"
              ></Field>
              <label>I am lactose intolerant</label>
            </div>
            <div>
              <label>Other restrictions:</label>
              <Field
                name="otherRestrictions"
                component="textarea"
                lines="2"
              ></Field>
            </div>
            <div>
              <button
                type="submit"
                color="primary"
                disabled={submitting || pristine}
              >
                Save
              </button>
            </div>
          </form>
        )}
      ></Form>
    );
  }

  return (
    <div className="container">
      <div>
        <h3 className="mt-4">User Profile</h3>
      </div>
      <RenderData />
    </div>
  );
};
