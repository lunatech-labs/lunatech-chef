import React from "react";
import { Form, Field } from "react-final-form";

export const AddLocation = (props) => {
  const required = (value) => (value ? undefined : "Required");
  const onSubmit = (values) => {
    props.addNewLocation(values);
  };

  function ShowError({ error }) {
    if (error) {
      return (
        <div>
          <h4>An error ocurred when adding new Location: {error}</h4>
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
        initialValues={{ city: "", country: "" }}
        render={({ handleSubmit, submitting, pristine }) => (
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
              <button
                type="submit"
                color="primary"
                disabled={submitting || pristine}
              >
                Add Location
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
        <h3 className="mt-4">New Location</h3>
      </div>
      <RenderData />
      <ShowError error={props.error} />
    </div>
  );
};
