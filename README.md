
File Store
--------


### What is this?

It's a component that allows you to store files by arbitrary ID.


### How do I use it?

Instantiate the `FileStore` class with a base path and manipulate content referenced by ID:

    fileStore = new FileStore(path);
    String id = "myarbitraryid";
    
    // Create
    FileInputStream content1 = new FileInputStream(file1);
    fileStore.create(id, content1);
    
    // Read
    InputStream stored = fileStore.read(id);
    
    // Update
    FileInputStream content2 = new FileInputStream(file2);
    fileStore.update(id, content2);
    
    // Delete
    fileStore.delete(id);

David Carboni

[https://github.com/davidcarboni/](https://github.com/davidcarboni/)
