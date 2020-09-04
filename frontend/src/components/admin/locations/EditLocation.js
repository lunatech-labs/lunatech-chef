import React from "react";
import { Form, Field } from "react-final-form";
import { useHistory } from "react-router-dom";

export const EditLocation = (props) => {
  let history = useHistory();
  const required = (value) => (value ? undefined : "Required");
  const onSubmit = (values) => {
    let editedLoc = {
      ...values,
      uuid: props.location.uuid,
    };
    props.editLocation(editedLoc);
    history.push("/alllocations");
  };

  function ShowError({ error }) {
    if (error) {
      return (
        <div>
          <h4>An error ocurred when editing a Location: {error}</h4>
        </div>
      );
    } else {
      return <div></div>;
    }
  }

  function RenderData() {
    return (
      <Form
        onSubmit={onSubmit}
        initialValues={{
          city: props.location.city,
          country: props.location.country,
        }}
        render={({ handleSubmit, submitting }) => (
          <form onSubmit={handleSubmit}>
            <div>
              <Field validate={required} name="city">
                {({ input, meta }) => (
                  <div>
                    <label>City</label>
                    <input {...input} type="text" placeholder="City" />
                    {meta.error && meta.touched && <span>{meta.error}</span>}
                  </div>
                )}
              </Field>
            </div>
            <div>
              <Field validate={required} name="country">
                {({ input, meta }) => (
                  <div>
                    <label>Country</label>
                    <input {...input} type="text" placeholder="Country" />
                    {meta.error && meta.touched && <span>{meta.error}</span>}
                  </div>
                )}
              </Field>
            </div>
            <div>
              <button type="submit" color="primary" disabled={submitting}>
                Save Location
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
        <h3 className="mt-4">Editing Location</h3>
      </div>
      <RenderData />
      <ShowError error={props.error} />
    </div>
  );
};
