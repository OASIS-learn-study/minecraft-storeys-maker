
export const upload = (content: any, path: string, token: string) => {
  let formData = new FormData();
  formData.append("file", new Blob([content]));
  fetch(path, {
    method: "POST",
    body: formData,
    headers: {
      Authorization: `bearer ${token}`,
    },
  });

}