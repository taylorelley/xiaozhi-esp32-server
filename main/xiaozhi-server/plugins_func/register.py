from config.logger import setup_logging
from enum import Enum

TAG = __name__

logger = setup_logging()


class ToolType(Enum):
    NONE = (1, "After the tool is called, do nothing else")
    WAIT = (2, "Call the tool and wait for the function to return")
    CHANGE_SYS_PROMPT = (3, "Modify the system prompt to switch role personality or responsibilities")
    SYSTEM_CTL = (
        4,
        "System control that affects normal dialogue flow, such as exit, playing music, etc.; the conn parameter is required",
    )
    IOT_CTL = (5, "IoT device control; the conn parameter is required")
    MCP_CLIENT = (6, "MCP client")

    def __init__(self, code, message):
        self.code = code
        self.message = message


class Action(Enum):
    ERROR = (-1, "Error")
    NOTFOUND = (0, "Function not found")
    NONE = (1, "Do nothing")
    RESPONSE = (2, "Reply directly")
    REQLLM = (3, "After calling the function, request the LLM to generate a reply")

    def __init__(self, code, message):
        self.code = code
        self.message = message


class ActionResponse:
    def __init__(self, action: Action, result=None, response=None):
        self.action = action  # Action type
        self.result = result  # Result produced by the action
        self.response = response  # Content of the direct reply


class FunctionItem:
    def __init__(self, name, description, func, type):
        self.name = name
        self.description = description
        self.func = func
        self.type = type


class DeviceTypeRegistry:
    """Device type registry for managing IoT device types and their functions"""

    def __init__(self):
        self.type_functions = {}  # type_signature -> {func_name: FunctionItem}

    def generate_device_type_id(self, descriptor):
        """Generate a type ID from the device capability descriptor"""
        properties = sorted(descriptor["properties"].keys())
        methods = sorted(descriptor["methods"].keys())
        # Use the combination of properties and methods as the unique identifier for the device type
        type_signature = (
            f"{descriptor['name']}:{','.join(properties)}:{','.join(methods)}"
        )
        return type_signature

    def get_device_functions(self, type_id):
        """Get all functions corresponding to a device type"""
        return self.type_functions.get(type_id, {})

    def register_device_type(self, type_id, functions):
        """Register a device type and its functions"""
        if type_id not in self.type_functions:
            self.type_functions[type_id] = functions


# Initialize the function registration dictionary
all_function_registry = {}


def register_function(name, desc, type=None):
    """Decorator that registers a function into the function registry dictionary"""

    def decorator(func):
        all_function_registry[name] = FunctionItem(name, desc, func, type)
        logger.bind(tag=TAG).debug(f"Function '{name}' loaded and ready to be registered")
        return func

    return decorator


def register_device_function(name, desc, type=None):
    """Decorator that registers a device-level function into the function registry dictionary"""

    def decorator(func):
        logger.bind(tag=TAG).debug(f"Device function '{name}' loaded")
        return func

    return decorator


class FunctionRegistry:
    def __init__(self):
        self.function_registry = {}
        self.logger = setup_logging()

    def register_function(self, name, func_item=None):
        # If func_item is provided, register it directly
        if func_item:
            self.function_registry[name] = func_item
            self.logger.bind(tag=TAG).debug(f"Function '{name}' registered directly")
            return func_item

        # Otherwise, look it up in all_function_registry
        func = all_function_registry.get(name)
        if not func:
            self.logger.bind(tag=TAG).error(f"Function '{name}' not found")
            return None
        self.function_registry[name] = func
        self.logger.bind(tag=TAG).debug(f"Function '{name}' registered successfully")
        return func

    def unregister_function(self, name):
        # Unregister a function, checking whether it exists
        if name not in self.function_registry:
            self.logger.bind(tag=TAG).error(f"Function '{name}' not found")
            return False
        self.function_registry.pop(name, None)
        self.logger.bind(tag=TAG).info(f"Function '{name}' unregistered successfully")
        return True

    def get_function(self, name):
        return self.function_registry.get(name)

    def get_all_functions(self):
        return self.function_registry

    def get_all_function_desc(self):
        return [func.description for _, func in self.function_registry.items()]
