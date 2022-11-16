import React from "react";
import { Form, Field } from "react-final-form";
import { useNavigate } from "react-router-dom";

export function AddLocation(props) {
  let navigate = useNavigate();
  const required = (value) => (value ? undefined : "Required");
  const onSubmit = (values) => {
    props.addNewLocation(values);
    navigate.push("/alllocations");
  };

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
    </div>
  );
};
