import React from "react";
import { Form, Field } from "react-final-form";
import { useNavigate } from "react-router-dom";

export function EditDish(props) {
  let navigate = useNavigate();
  const required = (value) => (value ? undefined : "Required");
  const onSubmit = (values) => {
    let editedDish = {
      ...values,
      uuid: props.dish.uuid,
    };
    props.editDish(editedDish);
    navigate.push("/alldishes");
  };

  function ShowError({ error }) {
    if (error) {
      return (
        <div>
          <h4>An error ocurred when editing a Dish: {error}</h4>
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
          name: props.dish.name,
          description: props.dish.description,
          isVegetarian: props.dish.isVegetarian,
          isHalal: props.dish.isHalal,
          hasNuts: props.dish.hasNuts,
          hasSeafood: props.dish.hasSeafood,
          hasPork: props.dish.hasPork,
          hasBeef: props.dish.hasBeef,
          isGlutenFree: props.dish.isGlutenFree,
          hasLactose: props.dish.hasLactose,
        }}
        render={({ handleSubmit, submitting }) => (
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
              <Field name="isHalal" component="input" type="checkbox"></Field>
              <label>Is halal</label>
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
              <Field
                name="hasLactose"
                component="input"
                type="checkbox"
              ></Field>
              <label>Contains lactose</label>
            </div>
            <div>
              <button type="submit" color="primary" disabled={submitting}>
                Save Dish
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
        <h3 className="mt-4">Editing Dish</h3>
      </div>
      <RenderData />
      <ShowError error={props.error} />
    </div>
  );
};
