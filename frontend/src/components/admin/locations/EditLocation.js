import React from "react";
import { Form, Field } from "react-final-form";
import { useNavigate, useLocation } from "react-router-dom";

export function EditLocation(props) {
  const required = (value) => (value ? undefined : "Required");

  const location = useLocation().state;

  const navigate = useNavigate();
  const onSubmit = (values) => {
    let editedLoc = {
      ...values,
      uuid: location.uuid,
    };
    props.editLocation(editedLoc);
    navigate("/alllocations");
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
          city: location.city,
          country: location.country,
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
