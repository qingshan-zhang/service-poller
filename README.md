- Stored added services into the database with columns:
   - url: url
   - name: hostName
   - user: cookie value of the add request
   - timestamp: time when the servie is added
- Added delete for individual service
- Send get request to each service for health check, and timeout for slow service
- Refresh the page at a fixed intervals to update the services status
- Validate service input and return 400 for invalid url
- Store and show added service for each user identified by cookie 

*TODOs*
- Add update on both frontend and backend
- Error response code handling on frontend, show alert message for 400 response caused by invalid url input.
- Add lock to avoid race condition
- Polish UI
