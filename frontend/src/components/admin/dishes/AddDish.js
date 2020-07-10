import React from "react";
import { Form, Field } from "react-final-form";

export const AddDish = (props) => {
  const required = (value) => (value ? undefined : "Required");
  const onSubmit = (values) => {
    props.addNewDish(values);
  };

  function ShowError({ error }) {
    if (error) {
      return (
        <div>
          <h4>An error ocurred when adding new Dish: {error}</h4>
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
          name: "",
          description: "",
          isVegetarian: false,
          hasNuts: false,
          hasSeafood: false,
          hasPork: false,
          hasBeef: false,
          isGlutenFree: false,
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
            <div>
              <Field validate={required} name="description">
                {({ input, meta }) => (
                  <div>
                    <label>Description</label>
                    <input {...input} type="text" placeholder="Description" />
                    {meta.error && meta.touched && <span>{meta.error}</span>}
                  </div>
                )}
              </Field>
            </div>
            <div>
              <label>Choose all that apply:</label>
            </div>
            <div>
              <Field
                name="isVegetarian"
                component="input"
                type="checkbox"
              ></Field>
              <label>Is vegetarian</label>
            </div>
            <div>
              <Field name="hasNuts" component="input" type="checkbox"></Field>
              <label>Contains nuts</label>
            </div>
            <div>
              <Field
                name="hasSeafood"
                component="input"
                type="checkbox"
              ></Field>
              <label>Contains seafood</label>
            </div>
            <div>
              <Field name="hasPork" component="input" type="checkbox"></Field>
              <label>Contains pork</label>
            </div>
            <div>
              <Field name="hasBeef" component="input" type="checkbox"></Field>
              <label>Contains beef</label>
            </div>
            <div>
              <Field
                name="isGlutenFree"
                component="input"
                type="checkbox"
              ></Field>
              <label>Is gluten free</label>
            </div>
            <div>
              <button
                type="submit"
                color="primary"
                disabled={submitting || pristine}
              >
                Add Dish
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
        <h3 className="mt-4">New Dish</h3>
      </div>
      <RenderData />
      <ShowError error={props.error} />
    </div>
  );
};
