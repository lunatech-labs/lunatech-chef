import React from "react";
import { Form, Field } from "react-final-form";

export const AddMenu = (props) => {
  const required = (value) => (value ? undefined : "Required");
  const onSubmit = (values) => {
    console.log("Adding menu");
    console.log(values);
    props.addNewMenu(values);
  };

  return (
    <div className="container">
      <div>
        <h3 className="mt-4">New Menu</h3>
      </div>
      <Form
        onSubmit={onSubmit}
        initialValues={{
          name: "",
          dishesUuids: [],
        }}
        render={({ handleSubmit, submitting, pristine }) => (
          <form onSubmit={handleSubmit}>
            <div>
              <Field validate={required} name="name">
                {({ input, meta }) => (
                  <div>
                    <label>Name</label>
                    <input {...input} type="text" placeholder="Name" />
                    {meta.error && meta.touched && <span>{meta.error}</span>}
                  </div>
                )}
              </Field>
            </div>
            {props.dishes.map((dish, index, arr) => {
              return (
                <div>
                  <Field
                    name="dishesUuids"
                    component="input"
                    type="checkbox"
                    value={dish.uuid}
                  ></Field>
                  <label>{dish.name}</label>
                </div>
              );
            })}

            <div>
              <button
                type="submit"
                color="primary"
                disabled={submitting || pristine}
              >
                Add Menu
              </button>
            </div>
          </form>
        )}
      ></Form>
    </div>
  );
};
